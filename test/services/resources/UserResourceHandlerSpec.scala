package services.resources

import db.PlayDBSpec
import models.User
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class UserResourceHandlerSpec extends PlayDBSpec with BeforeAndAfterEach with MockitoSugar {

  private var handler: UserResourceHandler = _

  implicit override lazy val app: play.api.Application = this.builder("User_resource_db").build()

  override def beforeEach(): Unit =
    this.handler = this.app.injector.instanceOf[UserResourceHandler]

  override def afterEach(): Unit = {
    val _ = Await.ready(this.handler.list map (_.map { kc =>
      Await.ready(this.handler.delete(kc.id.get), 1 second)
      kc
    }), 2 seconds)
  }

  "A UserResourceHandler" must {

    "count items on db" in {
      val count0 = Await.result(this.handler.count, 1 second)

      val item1 = User(None, "name1")
      val item2 = User(None, "name2")

      Await.ready(this.handler.save(item1), 1 second)
      Await.ready(this.handler.save(item2), 1 second)

      val count2 = Await.result(this.handler.count, 1 second)

      count0 mustBe 0
      count2 mustBe 2
    }

    "update items on db and find by id" in {

      val item1 = User(None, "name1")
      val item2 = User(None, "name2")

      val saved1 = Await.result(this.handler.save(item1), 1 second).copy(name = "updated name")
      Await.ready(this.handler.save(item2), 1 second)

      val updated = Await.result(this.handler.save(saved1), 1 second)
      val found   = Await.result(this.handler.get(saved1.id.get), 1 second)

      updated mustBe found.get
    }

    "delete items on db by id" in {
      val item1 = User(None, "name1")
      val item2 = User(None, "name2")

      val saved1 = Await.result(this.handler.save(item1), 1 second)
      Await.ready(this.handler.save(item2), 1 second)

      Await.ready(this.handler.delete(saved1.id.get), 1 second)
      val found = Await.result(this.handler.get(saved1.id.get), 1 second)

      found mustBe None
    }

    "list all items on db" in {
      val item1 = User(None, "name1")
      val item2 = User(None, "name2")

      val saved1 = Await.result(this.handler.save(item1), 1 second)
      val saved2 = Await.result(this.handler.save(item2), 1 second)

      val list = Await.result(this.handler.list, 1 second)

      list mustBe List(saved1, saved2)
    }

  }

}
