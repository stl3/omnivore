package app.omnivore.omnivore.ui.reader

import android.util.Log
import app.omnivore.omnivore.persistence.entities.SavedItem
import app.omnivore.omnivore.persistence.entities.Highlight
import com.google.gson.Gson

enum class WebFont(val displayText: String, val rawValue: String) {
  INTER("Inter", "Inter"),
  SYSTEM("System Default", "unset"),
  OPEN_DYSLEXIC("Open Dyslexic", "OpenDyslexic"),
  MERRIWEATHER("Merriweather", "Merriweather"),
  LORA("Lora", "Lora"),
  OPEN_SANS("Open Sans", "Open Sans"),
  ROBOTO("Roboto", "Roboto"),
  CRIMSON_TEXT("Crimson Text", "Crimson Text"),
  SOURCE_SERIF_PRO("Source Serif Pro", "Source Serif Pro"),
  NEWSREADER("Newsreader", "Newsreader"),
  LXGWWENKAI("LXGW WenKai", "LXGWWenKai"),
  ATKINSON_HYPERLEGIBLE("Atkinson Hyperlegible", "AtkinsonHyperlegible"),
  SOURCE_SANS_PRO("Source Sans Pro", "SourceSansPro"),
  IBM_PLEX_SANS("IBM Plex Sans", "IBMPlexSans"),
}

enum class ArticleContentStatus(val rawValue: String) {
  FAILED("FAILED"),
  PROCESSING("PROCESSING"),
  SUCCEEDED("SUCCEEDED"),
  UNKNOWN("UNKNOWN")
}

data class ArticleContent(
  val title: String,
  val htmlContent: String,
  val highlights: List<Highlight>,
  val contentStatus: String, // ArticleContentStatus,
  val objectID: String?, // whatever the Room Equivalent of objectID is
  val labelsJSONString: String
) {
  fun highlightsJSONString(): String {
    return Gson().toJson(highlights)
  }
}

data class WebReaderContent(
  val preferences: WebPreferences,
  val item: SavedItem,
  val articleContent: ArticleContent,
) {
  fun styledContent(): String {
    val savedAt = "\"${item.savedAt}\""
    val createdAt = "\"${item.createdAt}\""
    val publishedAt = if (item.publishDate != null) "\"${item.publishDate}\"" else "undefined"


    val textFontSize = preferences.textFontSize
    val highlightCssFilePath = "highlight${if (preferences.themeKey == "Dark") "-dark" else ""}.css"

    Log.d("theme", "current theme is: ${preferences.themeKey}")

    return """
          <!DOCTYPE html>
          <html>
            <head>
              <meta charset="utf-8" />
              <meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no' />
                <style>
                  @import url("$highlightCssFilePath");
                </style>
            </head>
            <body>
              <div id="root" />
              <div id='_omnivore-htmlContent'>
                ${articleContent.htmlContent}
              </div>
              <script type="text/javascript">
                window.omnivoreEnv = {
                  "NEXT_PUBLIC_APP_ENV": "prod",
                  "NEXT_PUBLIC_BASE_URL": "unset",
                  "NEXT_PUBLIC_SERVER_BASE_URL": "unset",
                  "NEXT_PUBLIC_HIGHLIGHTS_BASE_URL": "unset"
                }

                window.omnivoreArticle = {
                  id: "${item.savedItemId}",
                  linkId: "${item.savedItemId}",
                  slug: "${item.slug}",
                  createdAt: ${createdAt},
                  savedAt: ${savedAt},
                  publishedAt: ${publishedAt},
                  url: `${item.pageURLString}`,
                  title: `${articleContent.title.replace("`", "\\`")}`,
                  content: document.getElementById('_omnivore-htmlContent').innerHTML,
                  originalArticleUrl: "${item.publisherURLString}",
                  contentReader: "WEB",
                  readingProgressPercent: ${item.readingProgress},
                  readingProgressAnchorIndex: ${item.readingProgressAnchor},
                  labels: ${articleContent.labelsJSONString},
                  highlights: ${articleContent.highlightsJSONString()},
                }

                window.themeKey = "${preferences.themeKey}"
                window.fontSize = $textFontSize
                window.fontFamily = "${preferences.fontFamily.rawValue}"
                window.maxWidthPercentage = ${preferences.maxWidthPercentage}
                window.lineHeight = ${preferences.lineHeight}
                window.prefersHighContrastFont = ${preferences.prefersHighContrastText}
                window.justifyText = ${preferences.prefersJustifyText}
                window.enableHighlightBar = false
              </script>
              <script src="bundle.js"></script>
              <script src="mathJaxConfiguration.js" id="MathJax-script"></script>
              <script src="mathjax.js" id="MathJax-script"></script>
            </body>
          </html>
    """
  }
}
