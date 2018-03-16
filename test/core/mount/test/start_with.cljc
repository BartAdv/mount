(ns mount.test.start-with
  (:require
    #?@(:cljs [[cljs.test :as t :refer-macros [is are deftest testing use-fixtures]]
               [mount.core :as mount :refer-macros [defstate]]
               [tapp.websockets :refer [system-a]]
               [tapp.conf :refer [config]]
               [tapp.audit-log :refer [log]]]
        :clj  [[clojure.test :as t :refer [is are deftest testing use-fixtures]]
               [mount.core :as mount :refer [defstate]]
               [tapp.conf :refer [config]]
               [tapp.nyse :refer [conn]]
               [tapp.example :refer [nrepl]]])
   [mount.test.helper :refer [dval helper]]))

#?(:clj (alter-meta! *ns* assoc ::load false))

(defstate test-conn :start 42
                    :stop (constantly 0))

(defstate test-nrepl :start [])

#?(:cljs
  (deftest start-with

    (testing "should start with substitutes"
      (let [_ (mount/start-with {#'tapp.websockets/system-a "system-a-sub"
                                 #'mount.test.helper/helper "helper-sub"})]
        (is (map? (dval config)))
        (is (= "helper-sub" (dval helper)))
        (is (= "system-a-sub" (dval system-a)))
        (is (instance? datascript.db/DB @(dval log)))
        (mount/stop)))

    (testing "should start normally after start-with"
      (let [_ (mount/start)]
        (is (map? (dval config)))
        (is (instance? datascript.db/DB @(dval log)))
        (is (instance? js/WebSocket (dval system-a)))
        (is (= 42 (dval test-conn)))
        (is (vector? (dval test-nrepl)))
        (is (= :started (dval helper)))
        (mount/stop)))

    (testing "should start-without normally after start-with"
      (let [_ (mount/start-without #'mount.test.start-with/test-conn
                                   #'mount.test.start-with/test-nrepl)]
        (is (map? (dval config)))
        (is (instance? datascript.db/DB @(dval log)))
        (is (instance? js/WebSocket (dval system-a)))
        (is (= :started (dval helper)))
        (is (instance? mount.core.NotStartedState (dval test-conn)))
        (is (instance? mount.core.NotStartedState (dval test-nrepl)))
        (mount/stop)))))

#?(:clj
  (deftest start-with

    (testing "should start with substitutes"
      (let [_ (mount/start-with {#'tapp.nyse/conn "conn-sub"
                                 #'tapp.example/nrepl :nrepl-sub})]
        (is (map? (dval config)))
        (is (= :nrepl-sub (dval nrepl)))
        (is (= "conn-sub" (dval conn)))
        (mount/stop)))

    (testing "should start normally after start-with"
      (let [_ (mount/start)]
        (is (map? (dval config)))
        (is (instance? clojure.tools.nrepl.server.Server (dval nrepl)))
        (is (instance? datomic.peer.LocalConnection (dval conn)))
        (is (= (dval test-conn) 42))
        (is (vector? (dval test-nrepl)))
        (mount/stop)))

    (testing "should start-without normally after start-with"
      (let [_ (mount/start-without #'mount.test.start-with/test-conn
                                   #'mount.test.start-with/test-nrepl)]
        (is (map? (dval config)))
        (is (instance? clojure.tools.nrepl.server.Server (dval nrepl)))
        (is (instance? datomic.peer.LocalConnection (dval conn)))
        (is (instance? mount.core.NotStartedState (dval test-conn)))
        (is (instance? mount.core.NotStartedState (dval test-nrepl)))
        (mount/stop)))))
