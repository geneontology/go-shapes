TDIR = ../test_ttl/go_cams/
UTIL = ../util/

# run main tests
test: minitest
	python -m unittest tests/test_*.py

# same but via command line
all-%:
	python ./gocam_validator.py $(TDIR)/should_$*/*.ttl

# --
# experimental below here: new test cases in test/data
# --
# convention is:
# 
#  p-N : a test ttlite file that is expected to pass
#  f-N-XXX : a test ttlite file that is expected to fail (XXX is the shortname for the 'reason')
#
# Note that the f-N-XXX files are generated automatically from the p-N files

# test files are authored as ad-hoc ttlite format;
# these are compiled to 
tests/data/%.ttl: tests/data/%.ttlite $(UTIL)/cvt-ttlite.pl
	 $(UTIL)/cvt-ttlite.pl $< > $@.tmp && mv $@.tmp $@

MTDIR = tests/data
MINITEST =  $(patsubst $(MTDIR)/%.ttlite, %, $(wildcard $(MTDIR)/*.ttlite))

minitest: $(MINITEST)

#minitest: \
#  p-1 \
#  f-1-two-enabled_by \
#  f-1-occurs_in-gp


p-%: tests/data/p-%.ttl
	python ./gocam_validator.py $<

f-%: tests/data/f-%.ttl
	python ./gocam_validator.py $< && exit -1 || echo FAILED AS EXPECTED

.PRECIOUS: tests/data/%.ttl
