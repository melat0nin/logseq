(ns frontend.db.debug
  (:require [medley.core :as medley]
            [frontend.db.utils :as db-utils]
            [frontend.db :as db]
            [datascript.core :as d]
            [frontend.util :as util]))

;; shortcut for query a block with string ref
(defn qb
  [string-id]
  (db-utils/pull [:block/uuid (medley/uuid string-id)]))

(defn check-left-id-conflicts
  []
  (let [db (db/get-conn)
        blocks (->> (d/datoms db :avet :block/uuid)
                    (map :v)
                    (map (fn [id]
                           (let [e (db-utils/entity [:block/uuid id])]
                             (if (:block/name e)
                               nil
                               {:block/left (:db/id (:block/left e))
                                :block/parent (:db/id (:block/parent e))}))))
                    (remove nil?))
        count-1 (count blocks)
        count-2 (count (distinct blocks))
        result (filter #(> (second %) 1) (frequencies blocks))]
    (assert (= count-1 count-2) (util/format "Blocks count: %d, repeated blocks count: %d"
                                             count-1
                                             (- count-1 count-2)))))

(comment
  (defn debug!
    []
    (let [repos (->> (get-in @state/state [:me :repos])
                     (map :url))]
      (mapv (fn [repo]
              {:repo/current (state/get-current-repo)
               :repo repo
               :git/cloned? (cloned? repo)
               :git/status (get-key-value repo :git/status)
               :git/error (get-key-value repo :git/error)})
            repos))))
