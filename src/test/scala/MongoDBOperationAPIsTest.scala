import org.scalatest.FunSuite

/**
  * Created by singsand on 12/4/2016.
  */
class MongoDBOperationAPIsTest extends FunSuite {

  test("Check response returned when mongoDB is queried with incorrect collection name")
  {

    //call method to check response
    //println(MongoDBOperationAPIs.getRepoDetails("randomNameToCheckItDoesntExist")(0))
    assert(    MongoDBOperationAPIs.getRepoDetails("randomNameToCheckItDoesntExist").size==1)
    assert( MongoDBOperationAPIs.getRepoDetails("randomNameToCheckItDoesntExist")(0)(0).equals("0"))
  }

}
