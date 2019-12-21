(ns adventure.core
  (:gen-class))
(require '[clojure.string :as str])

(def init-map
  {:lobby {:desc "You start here. You have to get your final project done for CS 296. You only have 30 hours to complete the entire project. Since each move takes an hour, you only got 30 moves. You must choose wisely."
           :title "in the Siebel lobby"
           :dir {:west :einstein-bros}
           :contents #{}
           :energy-inc 0
           :proj-inc 0}
   :einstein-bros {:desc "You got caffeinated with +10 energy"
                   :title "at the Einstein Bros Bagels"
                   :dir {:east :lobby :west :ACM-room :upstairs :Mattox-office :downstairs :0216}
                   :contents #{}
                   :energy-inc 10
                   :proj-inc 0}
   :Mattox-office {:desc "Ask Prof. Mattox for help"
                   :title "in Prof. Mattox office"
                   :dir {:downstairs :einstein-bros}
                   :contents #{:information}
                   :energy-inc 10
                   :proj-inc 5}
   :0216 {:desc "It's office hours. Ask the TAs for help."
          :title "in Siebel 0216"
          :dir {:east :0224 :north :outside :upstairs :einstein-bros}
          :contents #{:help}
          :energy-inc 10
          :proj-inc 5}
   :ACM-room {:desc "Chillax and play board games with other CS students."
              :title "in the ACM room"
              :dir {:east :einstein-bros :north :ACM-back-room}
              :contents #{:board-game}
              :energy-inc 0
              :proj-inc 0}
   :0224 {:desc "You can start coding your project in this lab. You got 20% more project done at the expense of 30 energy."
          :title "in Siebel 0224"
          :dir {:west :0216}
          :contents #{}
          :energy-inc -30
          :proj-inc 20}
   :ACM-back-room {:desc "Congrats on finding the hidden ping-pong table in Siebel."
                   :title "in the ACM back room"
                   :dir {:south :ACM-room :north :outside}
                   :contents #{:ping-pong}
                   :energy-inc 0
                   :proj-inc 0}
   :outside {:desc "Enjoy the fresh air, working outside on your lappy is more fun. You got 30% more project done at the expense of 20 energy."
             :title "outside Siebel"
             :dir {:south :0216}
             :contents #{}
             :energy-inc -20
             :proj-inc 30}})

(def init-items
  {:ping-pong {:desc "Play with this set to get +30 energy!"
               :name "Ping pong set"
               :action "play"
               :knowledge-inc 0
               :energy-inc 30}
   :board-game {:desc "Play a game of Go to get +20 energy!"
                :name "Go Board"
                :action "play"
                :knowledge-inc 0
                :energy-inc 20}
   :information {:desc "Internalize to get +50 knowledge"
                 :name "Information"
                 :action "internalize"
                 :knowledge-inc 50
                 :energy-inc -10}
   :help {:desc "Utilize this help to gain +50 knowledge"
          :name "Ping pong set"
          :action "utilize"
          :knowledge-inc 50
          :energy-inc -20}})

(def init-adventurer
  {:location :lobby
   :inventory #{}
   :energy 10
   :knowledge 0
   :project-staus 0
   :seen #{}
   :turns-left 30})

(def init-state {:map init-map :adventurer init-adventurer :items init-items :result :undecided})

(defn canonicalize
  "Given an input string, strip out whitespaces, lowercase all words, and convert to a vector of keywords."
  [input]
  (map keyword (str/split (str/replace (str/lower-case input) #"[?.!]" "") #" +")))

(defn status [state]
  (let [location (get-in state [:adventurer :location])
        the-map (:map state)
        energy (get-in state [:adventurer :energy])
        know (get-in state [:adventurer :knowledge])
        proj (get-in state [:adventurer :project-staus])
        turns (get-in state [:adventurer :turns-left])
        dir (get-in state [:map location :dir])]
    (println (str "You are " (get-in state [:map location :title]) ". You can go in these directions: " (keys dir) "\nEnergy: " energy "\nKnowledge: " know "\nProject Status: " proj "\nTurns Left:" turns))
    (when-not ((get-in state [:adventurer :seen]) location)
      (println (-> the-map location :desc)))
    (update-in state [:adventurer :seen] #(conj % location))))

(defn go [state dir]
  (let [location (get-in state [:adventurer :location])
        dest ((get-in state [:map location :dir]) dir)
        e-inc (get-in state [:map dest :energy-inc])
        p-inc (get-in state [:map dest :proj-inc])]
    (if (nil? dest)
      (do (println "You can't go that way.")
          state)
      (assoc-in (update-in (update-in state [:adventurer :project-staus] + p-inc) [:adventurer :energy] + e-inc) [:adventurer :location] dest))
    ;(println e-inc dest)
    ))

(defn lookup-inventory [state]
  (println (str "Following items are present in your inventory:"
                (get-in state [:adventurer :inventory]))) state)

(defn examine-item [state item]
  (let [;location (get-in state [:adventurer :location])
        item_actual (get-in state [:adventurer :inventory item])
        it (get-in state [:items item_actual])]
    (if (nil? it)
      (do (println (str "No such item exists in your inventory")) state)
      (do (println (str "Here's the item description: " (:desc it))) state))))

(defn examine-room [state]
  (let [location (get-in state [:adventurer :location])
        conts (get-in state [:map location :contents])]
    (println (str "Here's the room description: \n" (get-in state [:map location :desc])))
    (println (str "Following items are present in the room:" conts))) state)

(defn pickup-item [state item]
  (let [location (get-in state [:adventurer :location])
        conts (get-in state [:adventurer :inventory])
        conts-room (get-in state [:map location :contents])]
    (if (conts-room item) (update-in
                           (assoc-in state [:adventurer :inventory] (conj conts item))
                           [:map location :contents] disj item) (do (println (str "This item is not present in the room.")) state))))

(defn drop-item [state item]
  (let [location (get-in state [:adventurer :location])
        conts (get-in state [:map location :contents])
        conts-i (get-in state [:adventurer :inventory])]
    (if (conts-i item)
      (assoc-in
       (update-in state [:adventurer :inventory] disj item)
       [:map location :contents] (conj conts item)) (do (println (str "No such item exists in your inventory")) state))))

(defn apply-action [state action item]
  (let [;location (get-in state [:adventurer :location])
        conts (get-in state [:adventurer :inventory])]
    (if (conts item) (if (= (get-in state [:items item :action]) (name action))
                       (update-in (update-in (update-in state [:adventurer :knowledge] + (get-in state [:items item :knowledge-inc])) [:adventurer :energy] + (get-in state [:items item :energy-inc])) [:adventurer :inventory] disj item)
                       (do (println (str "wrong action for " item ". Correct action is " action)) state))
        (do (println (str "Item not present in inventory")) state))))


(defn match [pattern input]
  (loop [pattern pattern
         input input
         vars '()]
    (cond (and (empty? pattern) (empty? input)) (reverse vars)
          (or (empty? pattern) (empty? input)) nil
          (= (first pattern) "@")
          (recur (rest pattern)
                 (rest input)
                 (cons (first input) vars))
          (= (first pattern) (first input))
          (recur (rest pattern)
                 (rest input)
                 vars)
          :fine-be-that-way nil)))

;; # Action Environment
;;
;; The runtime environment is a vector of the form
;;
;; ``` [ phrase action phrase action ...]```
;;
;; The "phrase" is a canonicalized vector of keywords.
;; (Remember that a keyword is a symbol starting with a colon, like :name.)
;; The `@` character will represent a variable.

(def init-env [[:go "@"] go [:examine :room] examine-room [:examine :item "@"] examine-item [:i] lookup-inventory [:pick :up "@"] pickup-item [:drop "@"] drop-item [:apply :action "@" :on "@"] apply-action])  ;; add your other functions here

(defn respond
  "Given a state and a canonicalized input vector, search for a matching phrase and call its corresponding action.
  If there is no match, return the original state and result \"I don't know what you mean.\""
  [state input-vector]
  (loop [idx 0]
    (if (>= idx (count init-env)) (do (println "bad input") state)
        (if-let [vars (match (init-env idx) input-vector)]
          (let [vect [(update-in state [:adventurer :turns-left] dec)]]
            (apply (init-env (inc idx)) (concat vect vars)))
          (recur (+ idx 2))))))

(defn update-status [state]
  (let [know (get-in state [:adventurer :knowledge])
        proj (get-in state [:adventurer :project-staus])
        e (get-in state [:adventurer :energy])
        turns (get-in state [:adventurer :turns-left])]
    (if (or (< e 0) (< turns 1)) (assoc-in state [:result] :lost) (if (and (> know 99) (> proj 99)) (assoc-in state [:result] :won) state))))

(defn -main
  "Initialize the adventure"
  []
  (println "\nYOU MAY LEARN THE GAME RULES FROM THE README FILE\n")
  (loop [local-state init-state]
    (cond (= :won (:result local-state)) (println (str "You won, congrats!"))
          (= :lost (:result local-state)) (println (str "You lost because of running out of either energy or turns :("))
          :else
          (let [pl (status local-state)
                _  (println "What do you want to do?")
                command (read-line)
                _ (println)]
            (cond (match [:exit] (canonicalize command)) (println (str "Thank you for playing!"))
                  :else
                  (recur (update-status (respond pl (canonicalize command)))))))))
