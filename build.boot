(defn get-creds'
  "Decrypting the file is quite slow (~100ms) so it's best to do it only once.
  Done as a memoized fn so that it's only done when it's needed."
  []
  (gpg-decrypt (clojure.java.io/file (System/getProperty "user.home") ".lein/credentials.clj.gpg") :as :edn))

(def get-creds (memoize get-creds'))

(configure-repositories!
  (fn [m]
    (time (merge m (some (fn [[regex cred]]
                           (if (re-find regex (:url m))
                             cred))
                         (get-creds))))))

(set-env!
  :repositories #(into % [["my.datomic.com" {:url "https://my.datomic.com/repo"
                                             :creds :gpg}]])
  :resource-paths #{"src" "resources"}
  :dependencies '[[org.clojure/clojure    "1.7.0"]
                  [boot/core              "2.3.0"      :scope "test"]

                  [com.datomic/datomic-pro "0.9.5327" :exclusions [org.slf4j/slf4j-nop]]
                  [metosin/compojure-api "0.15.0"]])

(task-options!
  pom {:project 'org.clojars.deraen/foobar
       :version "0.0.4"
       :description "Deploy test"
       :license {"The MIT License (MIT)" "http://opensource.org/licenses/mit-license.php"}
       :scm {:url "https://github.com/Deraen/boot-deploy-test"}
       :url "https://github.com/Deraen/boot-deploy-test"})

(deftask deploy []
  (comp
    (pom)
    (jar)
    (push :repo "clojars" :gpg-sign true)))
