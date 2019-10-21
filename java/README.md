# Java GO-CAM validator

Java code for validating GO-CAMs against the shex schema can be found in the minerva-core project at https://github.com/geneontology/minerva and can be accessed via the Minerva command line utility as seen in https://github.com/geneontology/minerva/blob/master/build-cli.sh 

You can run it like this:
 - git clone https://github.com/geneontology/minerva.git
 - cd minerva
 - git checkout dev
 - ./build-cli.sh
 - cd minerva-cli/
 - bin/minerva-cli.sh --validate-go-cams -s ../../shapes/go-cam-shapes.shex -m ../../shapes/go-cam-shapes.shapeMap -f ../../test_ttl/go_cams/should_pass/ -r ./shape_report_shouldpass.txt -expand -travis
 - bin/minerva-cli.sh --validate-go-cams -s ../../shapes/go-cam-shapes.shex -m ../../shapes/go-cam-shapes.shapeMap -f ../../test_ttl/go_cams/should_fail/ -r ./shape_report_shouldfail.txt -expand -travis -shouldfail

It will:

- Enrich each GO-CAM with subclass relations linking instances to root classes 
- then validate the resulting RDF models against the `go-cam-shapes.shex` schema using the `go-cam-shapes.shapeMap` shape map to look up the appropriate focus nodes.  
- It will show errors if the ttl files in the should_pass directories do not validate or if the ttl files in the should_fail directories do validate.  
