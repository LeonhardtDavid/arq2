import java.util.regex.Matcher

import io.gatling.core.check.regex.GroupExtractor

/**
  * Implicit extractors.
  */
package object extractors {

  implicit val longExtractor: GroupExtractor[Long] = (matcher: Matcher) => matcher.group(1).toLong

}
