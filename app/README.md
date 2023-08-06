# Kaizoyu! App
Source files for the Kaizoyu! mobile app.

## Release types

There are three release types (versions) of the app.

- `Release` signed, full version of the app. Version number is "x.x"
- `Beta` signed, beta version of the app. Version number is "x.xxx-BETA"
- `Debug` unsigned, development version of the app. Version number is "x.x-DEBUG"

Note: Releases of type `Debug` cannot (and shouldn't) use the in-app updater.

## Project contents

The source code is categorized in sections, detailed here:

- `com.astarivi.kaizoyu.core` source tree for internal classes related to core functionality, like 
shared components, models, storage managers, etc. These are modularized and separated.
- `com.astarivi.kaizoyu.utils` package containing shared utility classes. Also used as a way to
shorten intricate calls.
- `com.astarivi.kaizoyu.gui` package containing Fragment (and other UI) related classes. Every
activity that uses Fragments has this sub-package.
- `com.astarivi.kaizoyu.*` (excluding .core, .utils and .gui) every package represents an Activity,
which can be accessed from MainActivity, or that is shared across other activities.

Example: An activity created from a MainActivity Fragment, like the `More` tab, will be located at
`com.astarivi.kaizoyu.gui.more.activity`. The `gui` package means it's a Fragment inside MainActivity,
 `more` is the fragment itself, and `activity` is the activity we intend to create from the fragment.

Fragments inside the sub-package `.gui` of an Activity don't really need to be inside
their own packages, if they don't have other classes like recyclers or adapters, or if only
one of them has. Ex: `com.astarivi.kaizoyu.details.gui`

**TL;DR: Keep intricate logic inside of `.core` package, and use common sense to create UI classes.**

## Threading

Threading in Kaizoyu! is handled by the `com.astarivi.kaizoyu.utils.Threading` class, which is a
proxy to `com.astarivi.kaizoyu.core.threading.ThreadingAssistant`. There are two types of tasks
available:

- `com.astarivi.kaizoyu.utils.Threading.TASK.INSTANT` executes as soon as possible using a
thread from a cached thread pool. This results in near immediate execution.
- `com.astarivi.kaizoyu.utils.Threading.TASK.DATABASE` executes in a single thread, of which it
could have to wait for other tasks to finish execution first, before it actually runs. This
task type is used to access the database, and minimizes the risk of corruption by using a single,
managed thread.

Usage of this class is highly encouraged to keep consistency across the app. It's extremely easy to 
use, as `com.astarivi.kaizoyu.utils.Threading.submitTask` only requires two arguments, the task type
from the Enum, and a Runnable.

## Persistence

Check the `com.astarivi.kaizoyu.core.storage.PersistenceRepository` singleton for methods related
to this topic.

## Other stuff

This project uses Lombok. No plugin is needed for it to be recognized under Android Studio Canary. 
Your  mileage may vary with other IDEs. Try to use Lombok whenever possible to save on boilerplate.

Have fun!