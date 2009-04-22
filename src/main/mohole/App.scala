package mohole

import resources.script.Script
import scails.{Resources, ScailsApp}

class App extends ScailsApp (
  Map("/" -> "/scripts/list"),
  Resources.from(List(Script.get))
)