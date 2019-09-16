package controllers

import javax.inject.Singleton
import play.api.http.MimeTypes
import play.api.mvc._

/**
  * Health check controller with build information.
  */
@Singleton
class HealthCheckController extends InjectedController {

  /**
    * Health check method to inform application's status
    *
    * @return a status 200 with the build info
    */
  def check: Action[AnyContent] = Action {
    val buildInfo = controllers.BuildInfo.toJson
    Ok(buildInfo).as(MimeTypes.JSON)
  }

}
