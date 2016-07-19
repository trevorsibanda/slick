package slick.codegen
import java.io.File
import java.io.BufferedWriter
import java.io.FileWriter
import slick.SlickException

/** Output-related code-generation utilities. */
trait OutputHelpers{
  /** Patterns for outputing generated code**/
  trait Pattern
  object Pattern{
    case object UnPackaged extends Pattern
    case object Cake extends Pattern
    val Default = Cake
    case object Plain extends Pattern
    case object NestedObjects extends Pattern
  }

  def code: String

  case class GeneratedTable(val name: String, val schema: Option[String], val code: String)
  
  /** Dependency statements and imports for a packaging pattern
   *  @group Basic customization overrides
   */
  def dependencies: String
  /** Generated Tables **/
  val generatedTables: Seq[GeneratedTable]

  case class GeneratedCode(val code: String , val pkg: String, val fileName: Option[String] )
  /** Builds code based on the pattern used **/
  def patternBuilder( tables: Seq[GeneratedTable],  profile: String, pkg: String, container: String, parentType: Option[String] , pattern: Pattern ): Seq[GeneratedCode] = pattern match{
    case Pattern.UnPackaged => Seq( GeneratedCode(code= indent(tables.map{ t => t.code + "\n\n"}.mkString) ,pkg,None) )
    case Pattern.Plain => {
      Seq()
    }
    case Pattern.NestedObjects => {
      val schemas: Set[String] = tables.map{ _.schema.getOrElse("Db") }.toSet
      val pkgs = schemas.map{
        s => {
          val tbls = tables.filter( _.schema.getOrElse("Db") eq s )
          s"""
  object $s{
      ${indent( patternBuilder(tbls, profile, pkg, container, parentType, Pattern.UnPackaged).map{_.code}.mkString ) }
  }

          """.stripMargin
        }
      }
      Seq( GeneratedCode(code =s"""
package ${pkg}
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object ${container}{
  val profile: slick.jdbc.JdbcProfile = $profile
  import profile.api._
${indent(dependencies)}
${pkgs.map{ x => x }.mkString }

}

""".trim , pkg , None ) )
    }
    case Pattern.Cake => Seq( GeneratedCode(code = s"""
package ${pkg}
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object ${container} extends {
val profile = $profile
} with ${container}

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait ${container}${parentType.map(t => s" extends $t").getOrElse("")} {
val profile: slick.jdbc.JdbcProfile
import profile.api._
${indent(dependencies)}
${patternBuilder(tables, profile, pkg, container, parentType, Pattern.UnPackaged).map{_.code}.mkString}
}
    """.trim() , pkg , None ) )
  }

  /**
   *
   **/
  def packageName(pkg: String): String = s"Db$pkg"

  /** The parent type of the generated main trait. This can be overridden in subclasses. */
  def parentType: Option[String] = None

  /** Indents all but the first line of the given string.
   *  No indent is added to empty lines.
   */
  def indent(code: String): String

  /** Writes given content to a file.
   *  Ensures the file ends with a newline character.
   *  @group Output
   */
  def writeStringToFile(content: String, folder:String, pkg: String, fileName: String) {
    val folder2 : String = folder + "/" + (pkg.replace(".", "/")) + "/"
    new File(folder2).mkdirs()
    val file = new File( folder2+fileName )
    if (!file.exists()) {
      file.createNewFile();
    }
    val fw = new FileWriter(file.getAbsoluteFile());
    val bw = new BufferedWriter(fw);
    bw.write(content);
    if (!content.endsWith("\n")) bw.write("\n");
    bw.close();
  }

  /**
   * Generates code and writes it to a file.
   * Creates a folder structure for the given package inside the given srcFolder
   * and places the new file inside or overrides the existing one.
   * @group Output
   * @param folder target folder, in which the package structure folders are placed
   * @param profile Slick profile that is imported in the generated package (e.g. slick.jdbc.H2Profile)
   * @param pkg Scala package the generated code is placed in (a subfolder structure will be created within srcFolder)
   * @param container The name of a trait and an object the generated code will be placed in within the specified package.
   * @param fileName Name of the output file, to which the code will be written
   * @param pattern The pattern to use for generating the code
   */
  def writeToFile(profile: String, folder:String, pkg: String, container:String="Tables", fileName: String="Tables.scala", pattern: Pattern = Pattern.Default) {
    val generatedCode = patternBuilder(generatedTables, profile: String, pkg: String, container: String, parentType: Option[String] , pattern )
    (pattern , generatedCode) match{
      case (Pattern.Cake|Pattern.Plain|Pattern.NestedObjects,_) => generatedCode.map{ g => writeStringToFile(g.code , folder , pkg , fileName) }
    }
  }

  /**
   * Generates code providing the data model as trait and object in a Scala package
   * @group Basic customization overrides
   * @param profile Slick profile that is imported in the generated package (e.g. slick.jdbc.H2Profile)
   * @param pkg Scala package the generated code is placed in
   * @param container The name of a trait and an object the generated code will be placed in within the specified package.
   */
  def packageCode(profile: String, pkg: String, container: String, parentType: Option[String]) : String = {
   ""  
  }
}
