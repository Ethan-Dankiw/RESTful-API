package parser;

import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Parser Unit Tests")
@SelectPackages({ "parser.file" })
@IncludeClassNamePatterns(".*TestType")
public class _JavaParserTestSuite {

}
