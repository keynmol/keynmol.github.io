import $ivy.`com.lihaoyi::scalatags:0.9.1`

import $file.data
import data._
import com.indoorvivants.subatomic.Linker
import java.time.LocalDate

class Template(linker: Linker, tags: Iterable[TagPage]) {

  import scalatags.Text.all._
  import scalatags.Text.TypedTag

  def Nav(navigation: Navigation) = {
    div(
      navigation.items.sortBy(_.title).map {
        case Navigation.Item(title, _, selected) if selected =>
          p(strong(title))
        case Navigation.Item(title, url, _) =>
          p(a(href := url, title))
      }
    )
  }

  def rawHtml(rawHtml: String) = div(raw(rawHtml))

  def stylesheet(name: String) =
    link(
      rel := "stylesheet",
      href := linker.rooted(_ / "assets" / "styles" / name)
    )

  def scriptFile(name: String) =
    script(src := linker.rooted(_ / "assets" / "scripts" / name))

  def basePage(navigation: Option[Navigation], content: TypedTag[_]) = {
    val pageTitle = navigation
      .flatMap(_.items.find(_.selected))
      .map(_.title)
      .map(": " + _)
      .getOrElse("")
    html(
      head(
        scalatags.Text.tags2.title("Anton Sviridov" + pageTitle),
        stylesheet("monokai-sublime.min.css"),
        stylesheet("bootstrap.min.css"),
        stylesheet("site.css"),
        scriptFile("highlight.min.js"),
        scriptFile("r.min.js"),
        scriptFile("scala.min.js"),
        scriptFile("blog.js"),
        meta(charset := "UTF-8")
      ),
      body(
        div(
          cls := "d-flex justify-content-center flex-fill flex-grow",
          div(
            cls := "container",
            div(
              cls := "row",
              div(
                cls := "col-3 sidebar",
                h2(a(href := linker.root, "Indoor Vivants")),
                hr,
                about,
                staticNav,
                h4("projects"),
                projectsNav,
                hr,
                h4("tags"),
                tagCloud(tags),
                navigation match {
                  case Some(value) => div(hr, h4("posts"), Nav(value))
                  case None        => div()
                }
              ),
              div(cls := "col-8 contentside", content)
            )
          )
        )
      )
    )
  }

  def page(navigation: Navigation, content: TypedTag[_]) =
    basePage(Some(navigation), content)

  def blogPage(
      navigation: Navigation,
      title: String,
      tags: Iterable[String],
      content: TypedTag[_]
  ) = {
    val tagline = tags.toList.map { tag =>
      span(
        cls := "blog-tag",
        a(href := linker.rooted(_ / "tags" / s"$tag.html"), small(tag))
      )
    }
    page(navigation, div(h2(title), p(tagline), hr, content))
  }

  def tagPage(
      navigation: Navigation,
      tag: BlogTag,
      blogs: Iterable[BlogPost]
  ) = {
    page(
      navigation,
      div(
        h3(span("Posts tagged with ", span(cls := "blog-tag", tag.tag))),
        div(cls := "card-columns", blogs.map(blogCard).toVector)
      )
    )
  }

  def dateFormat(dt: LocalDate) = {
    dt.getYear + "-" + dt.getMonth + "-" + dt.getDayOfMonth()
  }

  def blogPostSummary(
      title: String,
      date: LocalDate,
      url: String
  ) = {
    li(h3(a(href := url, title)), dateFormat(date))
  }

  def tagCloud(
      tagPages: Iterable[TagPage]
  ) = {
    div(
      tagPages.toList.map { tagPage =>
        span(a(href := linker.resolve(tagPage), small(tagPage.tag.tag)), " ")
      }
    )
  }

  def blogCard(
      blogPost: BlogPost
  ) = {
    div(
      cls := "card",
      div(
        cls := "card-body",
        h5(
          cls := "card-title",
          a(href := linker.resolve(blogPost), blogPost.title)
        ),
        p(cls := "card-text", blogPost.description),
        div(
          cls := "card-text",
          small(dateFormat(blogPost.date))
        )
      )
    )
  }

  def indexPage(
      navigation: Navigation,
      blogs: Iterable[BlogPost]
  ) = {
    val (archived, modern) = blogs.partition(_.date.getYear < 2020)
    val newStuff =
      if (modern.nonEmpty)
        div(cls := "card-columns", modern.map(blogCard).toVector)
      else
        div(
          "Of course I spent all this time writing a static site generator and haven't actually written" +
            "a single blog post..."
        )

    basePage(
      None,
      div(
        h3("Blog posts"),
        newStuff,
        h3(cls := "text-muted", "Old posts"),
        div(cls := "card-columns", archived.map(blogCard).toVector)
      )
    )
  }

  def about =
    div(
      strong("Anton Sviridov"),
      p(
        "I love reinventing the wheel and I usually use Scala for that."
      )
    )

  def staticNav =
    ul(
      li(
        a(
          href := "https://github.com/keynmol/",
          "Github (personal)"
        )
      ),
      li(
        a(
          href := "https://twitter.com/velvetbaldmime/",
          "Tweettor"
        )
      )
    )

  def projectsNav =
    div(
      a(
        "Subatomic - static site generator",
        href := "https://subatomic.indoorvivants.com/"
      )
    )
}
