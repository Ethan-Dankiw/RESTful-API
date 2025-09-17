package utils;

import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Utility Unit Tests")
@SelectPackages({ "utils.time" })
@IncludeClassNamePatterns(".*TestType")
public class _JavaUtilsTestSuite {

}
