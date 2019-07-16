import os
import unittest
from types import ModuleType
from typing import List
from gocam_validator import validate
import logging

from rdflib import Graph

SKIP_LIST = [
    'test3.ttl',    # should pass
    'XBP-1 is a Cell-Nonautonomous regulator of Stress Resistance and Longevity.ttl',
    'fail_enabled_by_3.ttl', 'fail_no_evidence_4.ttl', 'fail_occurs_in_2.ttl'
]

class ValidateAgainstExamplesTestCase(unittest.TestCase):
    """
    Run GO-CAM validation test suite.

    Currently this is broken into two sets, with data files in two directories:

     - expected passes
     - expected fails
    """

    @staticmethod
    def _test_file_iter(subdir : str):
        print(f"FETCHING FILES IN {subdir}")
        
        test_files = os.path.abspath(os.path.join(os.path.dirname(__file__), '../../test_ttl/go_cams/' + subdir ))
        for root, subdirs, files in os.walk(test_files):
            for f in files:
                if 'typed' in f:
                    # we don't need these, redundant
                    continue
                # for now this runs over both the main test files and those in the typed_X subdir
                if f in SKIP_LIST or f.replace('typed_', '') in SKIP_LIST:
                    logging.info(f"Skipping {f} as is in skip list -- REMEMBER TO COME BACK TO THIS LATER")            
                    continue
                #if not f.endswith(".ttl")
                #    logging.info(f"Skipping {f} as is not ttl -- REMEMBER TO COME BACK TO THIS LATER")            
                #    continue
                print(f"Validating {f}")            
                yield validate(root + "/" + f)

    def test_positive_examples(self):
        """ Test positive examples succeed """
        n = 0
        for rpt in self._test_file_iter('should_pass'):
            print(f"TESTED+ : {rpt.rdf_file}")
            n += 1
            if not rpt.all_successful:
                print(f"FAILURES IN : {rpt.rdf_file}")
                for (inst, sc, reason) in rpt.fail_list:
                    print(f"FAIL: {inst} {sc} REASON: {reason}")
            self.assertTrue(rpt.all_successful)
        print(f"Ran {n} positive examples")

    def test_negative_examples(self):
        """ Test negative examples fail """
        n = 0
        for rpt in self._test_file_iter('should_fail'):
            print(f"TESTED- : {rpt.rdf_file}")
            n += 1
            if rpt.all_successful:
                print(f"Expected at least one of the following to FAIL in {rpt.rdf_file}")
                for (inst, sc, reason) in rpt.pass_list:
                    print(f"  PASS [unexpected]: {inst} {sc}")
            self.assertFalse(rpt.all_successful)
        print(f"Ran {n} negative examples")



if __name__ == '__main__':
    unittest.main()
