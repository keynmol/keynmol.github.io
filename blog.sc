import $ivy.`com.indoorvivants::subatomic-builders:0.0.4`
import subatomic.builders.blog._
import subatomic.builders._
import subatomic._

@main
def main(args: String*) = {
  interp.watch(os.pwd / "content")

  val blog = new Blog.App {
    def config =
      Blog(
        contentRoot = os.pwd / "content" / "blog",
        assetsRoot = Some(os.pwd / "content" / "assets"),
        name = "Indoor Vivants",
        tagline = Some(
          "Anton Sviridov. I love reinventing the wheel and I usually use Scala for that."
        ),
        links = Vector(
          "Github"   -> "https://github.com/keynmol",
          "Tweettor" -> "https://twitter.com/velvetbaldmime"
          // "Subatomic - barely a static site generator" -> "https://subatomic.indoorvivants.com/"
        ),
        highlightJS = Highlight.default
          .copy(
            languages = List("scala", "r"),
            theme = "monokai-sublime"
          )
      )

    override def extra(site: Site[Doc]) =
      site.addCopyOf(
        SiteRoot / "CNAME",
        os.pwd / "content" / "assets" / "CNAME"
      )
  }

  blog.main(args.toArray)
}
