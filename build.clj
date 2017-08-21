(require '[cljs.build.api :refer [build]])

(build "src" {:output-to "out/main.js"
              :main 'newlon_recommendation.core})
