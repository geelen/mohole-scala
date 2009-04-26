package mohole

import resources.script.{ScriptResource, Script}
import scails.{Resources, ScailsApp}

class App extends scails.App (
  Map("/" -> "/scripts/list"),
  Resources.from(List(ScriptResource))
)