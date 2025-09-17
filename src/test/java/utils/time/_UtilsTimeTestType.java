package utils.time;

import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Time Utils")
@SelectPackages("utils.time")
@IncludeClassNamePatterns(".*Test")
public class _UtilsTimeTestType {

}
