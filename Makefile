run:
	clj -X main/-main
t: 
	clj -M:test
repl:
	clojure -Sdeps '{:deps {nrepl/nrepl {:mvn/version "0.8.3"} cider/cider-nrepl {:mvn/version "0.25.7"} cider/piggieback {:mvn/version "0.5.2"} refactor-nrepl/refactor-nrepl {:mvn/version "2.5.1"}}}'  -m nrepl.cmdline --middleware "[cider.nrepl/cider-middleware refactor-nrepl.middleware/wrap-refactor cider.piggieback/wrap-cljs-repl]" --interactive
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
