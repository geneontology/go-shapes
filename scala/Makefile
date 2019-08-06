SHACLEX = JAVA_OPTS=-Xmx8G ./bin/shaclex
AMM = JAVA_OPTS=-Xmx8G ./bin/amm

PASS_DIR := ../test_ttl/go_cams/should_pass
PASS_FILES := $(wildcard $(PASS_DIR)/*.ttl)
PASS_ENRICHED := $(subst $(PASS_DIR),test/should_pass,$(PASS_FILES:.ttl=-enriched.ttl))
PASS_SHAPE_MAPS := $(PASS_ENRICHED:.ttl=.shapeMap)
FAIL_DIR := ../test_ttl/go_cams/should_fail
FAIL_FILES := $(wildcard $(FAIL_DIR)/*.ttl)
FAIL_ENRICHED := $(subst $(FAIL_DIR),test/should_fail,$(FAIL_FILES:.ttl=-enriched.ttl))
FAIL_SHAPE_MAPS := $(FAIL_ENRICHED:.ttl=.shapeMap)

test/%-enriched.ttl: ../test_ttl/go_cams/%.ttl
	mkdir -p $(dir $@) &&\
	$(AMM) enrich-with-superclasses.sc --infile $< --outfile $@

.PRECIOUS: test/%-enriched.ttl

%.shapeMap: %.ttl
	$(SHACLEX) \
	--schema ../shapes/go-cam-shapes.shex \
	--schemaFormat SHEXC \
	--shapeMap ../shapes/go-cam-shapes.shapeMap \
	--data $< \
	--trigger SHAPEMAP \
	--no-showLog \
	--no-showData \
	--no-showResult \
	--no-explain \
	--outFile $@ &&\
	grep $(if $(findstring should_pass,$@),-v,) '@!' $@ # Give an error result if the shapeMap does or doesn't contain unmatched shapes

test-pass: $(PASS_SHAPE_MAPS)

test-fail: $(FAIL_SHAPE_MAPS)

test: test-pass test-fail
