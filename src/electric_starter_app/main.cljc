(ns electric-starter-app.main
  (:require [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [hyperfiddle.electric-svg :as svg]))

#?(:clj (def !pointers (atom {})))

(e/defn Cursor []
  (svg/svg
    (dom/props {:width "60px" :height "60px" :viewBox "0 0 24 24" :fill "none"})
    (svg/path
      (dom/props
        {:fill "#0F0F0F"
         :fill-rule "evenodd"
         :clip-rule "evenodd"
         :d "M1.50001 4.07491C0.897091 2.46714 2.46715 0.897094 4.07491 1.50001L21.2155 7.92774C23.1217 8.64256 22.8657 11.4162 20.8609 11.77L13.1336 13.1336L11.77 20.8609C11.4162 22.8657 8.64255 23.1217 7.92774 21.2155L1.50001 4.07491ZM3.37267 3.37267L9.8004 20.5133L11.164 12.786C11.3101 11.9582 11.9582 11.3101 12.786 11.164L20.5133 9.8004L3.37267 3.37267Z"}))))

(e/defn Main [ring-request]
  (e/server
    (let [session-id (get-in ring-request [:headers "sec-websocket-key"])]
      (e/client
        (let [pointers (e/server (e/watch !pointers))]
          (binding [dom/node js/document.body]
            (e/server (e/on-unmount #(swap! !pointers dissoc session-id)))

            (dom/div
              (dom/props {:style {:width "100vw" :height "100vh" :cursor "none"}})

              (svg/svg
                (dom/props {:width "100%" :height "100%"})

                (dom/on "mousemove" (e/fn [e]
                                      (let [x (.-x e)
                                            y (.-y e)]
                                        (e/server
                                          (swap! !pointers assoc session-id [x y])
                                          nil))))

                (svg/g
                  (svg/text
                    (dom/props {:x 0 :y 0 :font-size 40 :text-anchor "top" :dy 40})
                    (dom/span
                      (dom/text "Multiplayer Mouse Pointers in Electric Clojure"))))

                (e/for-by first [[sid [x y]] pointers]
                  (svg/g
                    (dom/props {:transform (str "translate(" x "," y ")")})
                    (Cursor.)))))))))))
