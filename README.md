Automata
========

A library that implements formal finite state automata.

If you are looking for an efficient and feature complete linear-time regular expression engine, then this is not the project for you. [Google's re2](https://github.com/google/re2) will very likely do a better job at that.

However if you are trying to convert regular expressions into FSAs in order to proof certain properties of the regular expressions and are not too tight on performance requirements, this could be the project you are looking for.

What works?
-----------

- Converting simple ("formal") regular expressions into NFAs
- Converting Java's Patterns (most of the regular syntax is supported) into NFAs
- Converting NFAs into DFAs
- Various operations on DFAs and/or NFAs:
  - minimizing DFAs
  - completing DFAs
  - combining multiple automata into a one (e.g. intersection)
  - complements
- simple text matching

Possible future features
------------------------

- Transition events
- ???

Inspired by
-----------

[Prof. Dr. Karl Stroetmann](https://github.com/karlstroetmann)'s [Formal Languages Lecture](https://github.com/karlstroetmann/Formal-Languages)