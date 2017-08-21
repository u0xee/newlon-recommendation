(require 'cljs.repl
         '[cljs.build.api :refer [build]]
         '[cljs.repl.browser :refer [repl-env]])

(build "src" {:output-to "out/main.js"
              :main 'newlon_recommendation.core
              :browser-repl true
              :verbose true})

(cljs.repl/repl (repl-env)
                :watch "src"
                :output-dir "out")
