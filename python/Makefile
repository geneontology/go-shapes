TDIR = ../test_ttl/go_cams/

test:
	python -m unittest tests/test_*.py

all-%:
	python ./gocam_validator.py $(TDIR)/should_$*/*.ttl

p-%: tests/data/p-%.ttl
	python ./gocam_validator.py $<

f-%: tests/data/f-%.ttl
	python ./gocam_validator.py $< || exit -1

tests/data/%.ttl: tests/data/%.ttlite
	 ./cvt-ttlite.pl $< > $@
