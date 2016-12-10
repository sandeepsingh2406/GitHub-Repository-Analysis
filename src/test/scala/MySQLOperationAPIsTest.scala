import org.scalatest.FunSuite

/**
  * Created by singsand on 12/4/2016.
  */
class MySQLOperationAPIsTest extends FunSuite {

  test("insert a repo into repocommitstable and later check if it exists"){

    //call method to test and later assert
    MySQLOperationAPIs.insertTopRepoCommitsTable("testrepo",123,0,0)

    assert(MySQLOperationAPIs.checkRow("testrepo"))
    assert(!MySQLOperationAPIs.checkRow("checkingARepoTableThatShouldNotExists"))
  }
}
