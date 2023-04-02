What's happening

* There's a HTML table on the frontend, backed by a backend "query" which is an ordinary Clojure function on the server.
* Typing into the frontend input causes the backend query to rerun and update the table.
* The code arbitrarily nests client and server calls.

Key ideas

* **direct query/view composition**: the query expression on the server composes directly with the frontend view expression that renders it, unifying your code into one paradigm, promoting readability, and making it easier to maintain and refactor the interactions between client and server components.
* **query can be any function**: collections, SQL resultset, whatever
* **reactive-for**: The table rows are renderered by a for loop. Reactive loops are efficient and recompute branches only precisely when needed.
* **network planner**: values are only transferred between sites when and if they are used. The `system-props` collection is never actually accessed from a client region and therefore never escapes the server, despite being lexically available.

Novel forms

* `ui/input`: a text input control with "batteries included" loading/syncing state.
* `e/for-by`: a reactive map operator, stabilized to bind each child branch state (e.g. DOM element) to an entity in the collection by id (provided by userland fn - similar to [React.js key](https://stackoverflow.com/questions/28329382/understanding-unique-keys-for-array-children-in-react-js/43892905#43892905)).

Reactive for details

* `e/for-by` ensures that each table row is bound to a logical element of the collection, and only touched when a row dependency changes.
* Notice there is a `println` inside the for loop. This is so you can see the efficient rendering in the browser console. 
* Open the browser console now and confirm for yourself:
  * On initial render, each row is rendered once
  * Slowly input "java.class.path"
  * As you narrow the filter, no rows are recomputed. (The existing dom is reused, so there is nothing to recompute because neither `k` nor `v` have changed for that row.)
  * Slowly backspace, one char at a time
  * As you widen the filter, rows are computed as they come back. That's because they were unmounted and discarded!
  * Quiz: Try setting an inline style "background-color: red" on element "java.class.path". When is the style retained? When is the style lost?

Reasoning about network transfer

* Look at which remote scope values are closed over and accessed.
* Only remote access is transferred. Mere *availability* in scope does not transfer.
* In the `e/for-by`, `k` and `v` exist in a server scope, and yet are accessed from a client scope.
* Electric tracks this and sends a stream of individual `k` and `v` updates over network.
* The collection value `system-props` is not accessed from client scope, so Electric will not move it. Values are only moved if they are accessed.

For more information about reactive network efficiency, see [this clojureverse answer](https://clojureverse.org/t/electric-clojure-a-signals-dsl-for-fullstack-web-ui/9788/32?u=dustingetz).