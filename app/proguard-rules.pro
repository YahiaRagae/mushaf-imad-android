# The app deliberately adds NO library-specific keep rules.
#
# mushaf-core ships its own consumer ProGuard rules inside the AAR (proguard.txt),
# covering Realm, Koin and Media3. If a minified release of this app breaks, that
# is a library bug and it must be fixed in the library's consumer rules - not
# papered over here. Adding a keep rule for the library in this file would make
# this app stop being an honest consumer, which is exactly how the WAKE_LOCK bug
# went unnoticed in the old sample app.
