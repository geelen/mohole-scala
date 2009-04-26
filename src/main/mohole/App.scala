package mohole

import resources.script.{ScriptResource, Script}
import scails.{Resources, ScailsApp}

class App extends scails.demo.App (
  Map("/" -> "/scripts/list"),
  Resources.from(List(ScriptResource))
)