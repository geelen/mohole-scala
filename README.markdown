# slinky2-appengine-demo

This should be buildable and deployable to google's app engine, which is pretty pro I think you will find.

## What you need to do

  * Edit the appengine-web.xml file and make the application one of yours
  * Have scala in /opt/scala and the app engine sdk in /opt/slinky2-appengine-demo
  * Run ''ant deploy'' to make sure everything compiles
  * ''ant runserver'' to test locally
  * ''ant update'' to push it up to google!