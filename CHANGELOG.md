v1.0.0.201101161822
===================
- Added the ability to clean up the submission working directory (issue #113).
- Split `SVNManager` into `LocalSVNManager` and `RemoteSVNManager` (issue #113).
- Changed the SVN repository to assembla when running in test or debug mode (issue #105).
- Disabled the check box for turning off the UDC capture.
- Turned on UDC capture by default for all workspaces. And, let the user choose to submit UDC data to CodingSpectator but not the Eclipse server.
- Made CodingSpectator collect the UDC data even though it doesn't get uploaded to the Eclipse server. (issue #55).
- Prevented CodingSpectator from uploading data while Eclipse is about to shut down (issue #121).
- Fixed the `UnsupportedOperationException` in the instrumentation of the rename compilation unit refactoring (issue #118).
- Made `MoverProcessor` and `RenameProcessor` use delegation instead of inner classes (issue #118).
- Forced the menu item for extract method refactoring to be enabled in test mode in order to make SWTBot happy (issue #81).
- Added automated UI tests for the rename compilation unit refactoring (issue #122).
- Wrote automated UI tests for the following refactorings: move static field, move static method, move instance method, move compilation unit (issue #124).
- Self-signed the jar files (issue #133).

v1.0.0.201011241815
===================
- Copied UDC data to the watched directory of CodingSpectator just before UDC uploads its data.

v1.0.0.201011201702
===================
- Captured failures of refactorings before the refactoring wizard. These early failures make the refactorings unavailable. (See [issue #30](https://github.com/vazexqi/CodingSpectator/issues/30) for more details).
- Captured refactorings that programmers perform from quick assist (See [issue #109](https://github.com/vazexqi/CodingSpectator/issues/109) for more details).

v1.0.0.201011161607
===================
- Fixed a bug that logged a refactoring as both performed and canceled (See [issue #110](https://github.com/vazexqi/CodingSpectator/issues/110) for more details).

v1.0.0.201011111502
===================
- Added the support for the move refactoring.
- Adapted the automated refactoring tests.

v1.0.0.201011031917
===================
- Supported the rename refactoring.

v1.0.0.201011022017
===================
- Made it possible to disable the startup event in the target platform.

v1.0.0.201010211411
===================
- Patched org.eclipse.compare.
- Improved secure storage of authentication information.

v1.0.0.201010181311
===================
- Made the feature patches optional to coexist with other plugins.

v1.0.0.201010151736
===================
- Added CodingTracker to CodingSpectator.
- Added an extension to listen to pre and post submit events.

v1.0.0.201010071957
===================
- Added the support for the following refactorings: extract constant, extract method, extract local variable, inline constant, inline method, inline local variable, introduce factory, convert local variable to field.

