run:
	clj -X main/-main
repl:
	clj -Sdeps '{:deps {nrepl/nrepl {:mvn/version "0.5.3"}}}' -m nrepl.cmdline

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
