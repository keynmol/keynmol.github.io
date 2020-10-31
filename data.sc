import $ivy.`com.indoorvivants::subatomic:0.0.3`

import com.indoorvivants.subatomic._
import java.time.LocalDate

// ----- Data models for content
case class BlogTag(tag: String)

sealed trait Content extends Product with Serializable
case class BlogPost(
    title: String,
    tags: Set[BlogTag],
    date: LocalDate,
    content: Text,
    description: String = ""
)                                                             extends Content
case class SitePage(title: String, content: MarkdownText)     extends Content
case class StaticFile(file: os.Path)                          extends Content
case class TagPage(tag: BlogTag, blogPosts: Vector[BlogPost]) extends Content
case class IndexPage(blogs: Vector[BlogPost])                 extends Content

sealed trait Text extends Product with Serializable
case class MdocText(file: os.Path, dependencies: Set[String] = Set.empty)
    extends Text
case class MarkdownText(file: os.Path) extends Text

//------- Data models for the site

type Title    = String
type URL      = String
type Selected = Boolean
case class Navigation(items: Seq[Navigation.Item])
object Navigation {
  case class Item(title: String, url: String, selected: Boolean)

  def state(
      linker: Linker,
      content: Vector[(SitePath, Content)]
  ): (SitePath, Content) => Navigation = { (_, currentContent) =>
    val items = content.collect {
      case (path, bp: BlogPost) =>
        Navigation.Item(
          bp.title,
          linker.rooted(_ / path.toRelPath),
          currentContent == bp
        )
    }

    Navigation(items)
  }
}

//------ Content itself

def blogs(ContentRoot: os.Path): Vector[(SitePath, BlogPost)] = {
  // Vector[BlogPost](
  val googleSearchHistory = BlogPost(
    "Google search history analysis",
    tags = Set(
      BlogTag("R"),
      BlogTag("python"),
      BlogTag("data-analysis"),
      BlogTag("stocks"),
      BlogTag("ggplot2")
    ),
    date = LocalDate.of(2015, 5, 31),
    content =
      MarkdownText(ContentRoot / "blog" / "google-search-history-analysis.md")
  )

  val visualisingTimeSeries = BlogPost(
    "Visualising timeseries: stocks data and global trends",
    tags = Set(
      BlogTag("R"),
      BlogTag("python"),
      BlogTag("data-analysis"),
      BlogTag("ggplot2")
    ),
    date = LocalDate.of(2015, 6, 10),
    content = MarkdownText(
      ContentRoot / "blog" / "visualising-real-world-time-series.md"
    )
  )

  val testMdoc = BlogPost(
    "Test mdoc blog",
    tags = Set(BlogTag("scala")),
    date = LocalDate.now(),
    content = MdocText(
      ContentRoot / "blog" / "test-mdoc.md",
      dependencies = Set("org.typelevel::cats-effect:2.1.4")
    )
  )

  Vector(
    SiteRoot / "blog" / "visualising-time-series.html"        -> visualisingTimeSeries,
    SiteRoot / "blog" / "google-search-history-analysis.html" -> googleSearchHistory,
    SiteRoot / "blog" / "test-mdoc.html"                      -> testMdoc
  )
}

def pages(blogs: Vector[(SitePath, BlogPost)]) =
  Vector(
    SiteRoot / "index.html" -> IndexPage(blogs.map(_._2))
  )

def statics(ContentRoot: os.Path) = {
  os.walk(ContentRoot / "assets").map { path =>
    if (path.endsWith(os.RelPath("CNAME")))
      SiteRoot / "CNAME" -> StaticFile(path)
    else
      SiteRoot / path.relativeTo(ContentRoot) -> StaticFile(path)
  }
}

def tagPages(blogs: Vector[(SitePath, Content)]) = {
  val blogposts: Vector[(SitePath, BlogPost)] = blogs.collect {
    case (path, bp: BlogPost) => path -> bp
  }

  val uniqueTags = blogposts.flatMap(_._2.tags)

  uniqueTags.toVector.sortBy(_.tag).map { tag =>
    val contentWithTag = blogposts.map(_._2).filter(_.tags.contains(tag))

    SiteRoot / "tags" / s"${tag.tag}.html" ->
      TagPage(tag, contentWithTag)
  }

}

def Content(ContentRoot: os.Path): Vector[(SitePath, Content)] =
  blogs(ContentRoot) ++
    pages(blogs(ContentRoot)) ++
    statics(ContentRoot) ++
    tagPages(blogs(ContentRoot))
