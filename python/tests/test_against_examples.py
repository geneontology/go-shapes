import os
import unittest
from types import ModuleType
from typing import List
from gocam_validator import validate
import logging

from rdflib import Graph

SKIP_LIST = ['fail_no_evidence_4.ttl', 'fail_enabled_by_3.ttl', 'fail_IRE1-mediated_6.ttl', 'fail_part_of_1.ttl']

class ValidateAgainstExamplesTestCase(unittest.TestCase):
    """
    Run GO-CAM validation test suite.

    Currently this is broken into two sets, with data files in two directories:

     - expected passes
     - expected fails
    """

    @staticmethod
    def _test_file_iter(subdir : str):
        test_files = os.path.abspath(os.path.join(os.path.dirname(__file__), '../../test_ttl/go_cams/' + subdir ))
        for root, subdirs, files in os.walk(test_files):
            for f in files:
                if f in SKIP_LIST:
                    logging.info(f"Skipping {f} -- REMEMBER TO COME BACK TO THIS LATER")            
                    continue
                logging.info(f"Validating {f}")            
                yield validate(root + "/" + f)

    def test_positive_examples(self):
        """ Test positive examples succeed """
        n = 0
        for rpt in self._test_file_iter('should_pass'):
            n += 1
            if not rpt.all_successful:
                for (inst, sc, reason) in rpt.fail_list:
                    print(f"FAIL: {inst} {sc} REASON: {reason}")
            self.assertTrue(rpt.all_successful)
        print(f"Ran {n} positive examples")

    @unittest.skip("not done yet")                
    def test_negative_examples(self):
        """ Test negative examples fail """
        n = 0
        for rpt in self._test_file_iter('should_fail'):
            n += 1
            if rpt.all_successful:
                print(f"Expected at least one of the following to FAIL in {rpt.rdf_file}")
                for (inst, sc, reason) in rpt.pass_list:
                    print(f"  PASS [unexpected]: {inst} {sc}")
            self.assertFalse(rpt.all_successful)
        print(f"Ran {n} negative examples")


if __name__ == '__main__':
    unittest.main()
