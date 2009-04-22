package scails

object Utils {
  def say[A](a: A) = doc("Mohole!", a)

  def doc[A](title: String, a: A) =
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
        <title>{ title }</title>
      </head>
      <body>
        { a }
      </body>
    </html>
}