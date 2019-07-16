TDIR = ../test_ttl/go_cams/
UTIL = ../util/

# run main tests
test:
	python -m unittest tests/test_*.py

# same but via command line
all-%:
	python ./gocam_validator.py $(TDIR)/should_$*/*.ttl

# --
# experimental below here: new test cases in test/data
# --

# test files are authored as ad-hoc ttlite format;
# these are compiled to 
tests/data/%.ttl: tests/data/%.ttlite $(UTIL)/cvt-ttlite.pl
	 $(UTIL)/cvt-ttlite.pl $< > $@.tmp && mv $@.tmp $@

p-%: tests/data/p-%.ttl
	python ./gocam_validator.py $<

f-%: tests/data/f-%.ttl
	python ./gocam_validator.py $< && exit -1 || echo FAILED AS EXPECTED

.PRECIOUS: tests/data/%.ttl
