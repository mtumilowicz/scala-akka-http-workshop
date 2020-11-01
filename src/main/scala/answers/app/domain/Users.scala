package answers.app.domain

final case class Users(raw: Seq[User]) {
  def each[T](f: User => T): Seq[T] = raw.map(f)
}