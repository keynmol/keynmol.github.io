import $ivy.`com.indoorvivants::subatomic:0.0.3`
import $file.template
import $file.data

import com.indoorvivants.subatomic._
import template.Template
import data._

@main
def main(
    disableMdoc: Boolean = false,
    destination: os.Path = os.pwd / "_site"
) = {
  interp.watch(os.pwd / "content")

  buildSite(
    contentRoot = os.pwd / "content",
    siteBase = SiteRoot,
    destination = os.pwd / "_site",
    disableMdoc = disableMdoc
  )
}

// Actually putting everything together
def buildSite(
    contentRoot: os.Path,
    siteBase: SitePath,
    destination: os.Path,
    disableMdoc: Boolean
) = {
  val content = Content(contentRoot)
  val linker  = new Linker(content, siteBase)

  val mdocProc = new MdocProcessor()
  val markdown = Markdown(RelativizeLinksExtension(siteBase))

  val template = new Template(
    linker,
    content
      .map(_._2)
      .collect {
        case tg: TagPage => tg
      }
      .toSet
  )

  ammonite.ops.rm(destination)
  os.makeDir.all(destination)

  def processMdoc(path: os.Path, deps: Set[String]) = {
    if (disableMdoc) path else mdocProc.process(path, deps)
  }

  Site.build1[Content, Navigation](destination)(
    content,
    Navigation.state(linker, content)
  ) {
    // Rendering a mdoc-based post is a bit more involved
    case (_, bp: BlogPost, navigation) =>
      val markdownContent = bp.content match {
        case MarkdownText(file)   => file
        case MdocText(file, deps) => processMdoc(file, deps)
      }

      Some(
        Page(
          template
            .blogPage(
              navigation,
              bp.title,
              bp.tags.map(_.tag),
              template.rawHtml(
                markdown.renderToString(markdownContent)
              )
            )
            .render
        )
      )

    // Rendering regular pages is even simpler
    case (_, SitePage(title, content), navigation) =>
      Some(
        Page(
          template
            .page(
              navigation,
              template.rawHtml(markdown.renderToString(content.file))
            )
            .render
        )
      )

    case (_, TagPage(tag, blogPosts), navigation) =>
      Some(
        Page(
          template
            .tagPage(
              navigation,
              tag,
              blogPosts
            )
            .render
        )
      )
    case (_, IndexPage(blogs), navigation) =>
      Some(
        Page(
          template
            .indexPage(
              navigation,
              blogs
            )
            .render
        )
      )

    case (_, sf: StaticFile, _) =>
      Some(CopyOf(sf.file))
  }
}
