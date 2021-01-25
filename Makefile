run:
	clj -X main/-main
repl:
	clj -Sdeps '{:deps {nrepl/nrepl {:mvn/version "0.5.3"}}}' -m nrepl.cmdline
cljs:
	clojure -Sdeps '{:deps {nrepl/nrepl {:mvn/version "0.8.3"} cider/cider-nrepl {:mvn/version "0.25.7"} cider/piggieback {:mvn/version "0.5.2"}}}'  -m nrepl.cmdline --middleware "[cider.nrepl/cider-middleware cider.piggieback/wrap-cljs-repl]"

db.run:
	docker run \
	--name=local-postgres \
	--rm \
	-e POSTGRES_USER=postgres \
	-e POSTGRES_PASSWORD=mysecretpassword \
	-d \
	-p 5432:5432 \
	-v $(HOME)/docker/volumes/postgres:/var/lib/postgresql/data \
	postgres:alpine
db.connection:	
	docker exec -it local-postgres psql -U postgres
