# Comments

Follow Google's style-guide philosophy on comments: comments explain *why*,
not *what* — the code itself should say what it does. A comment is a sign
the code couldn't say something on its own; write the comment instead of
assuming the reader will figure it out, but don't write one just to narrate
what the next line already makes obvious.

A comment is necessary only when it explains something the code can't show
by itself:

- **Why, not what** — a non-obvious domain constraint or reasoning behind a
  decision (e.g. "bookings can never span a day, so a `bookedFrom`-only
  check is correct here" — not "// loop through bookings").
- **A rejected alternative or tradeoff** worth recording so no one "fixes"
  it back to the simpler-looking approach later (e.g. why Specifications
  were used instead of raw JPQL for optional filters).
- **A non-obvious contract** — Javadoc `@param`/`@return` clarifying
  behavior that isn't clear from the signature alone (e.g. that `dateFrom`/
  `dateTo` are each independently optional).

Banned regardless of the above:

- **No ticket/issue numbers in code comments** (e.g. "// BOO-13: ..."). It
  ties the code to a tracker entry that will rot or get renumbered — if the
  reasoning matters, write it in the comment itself, not as a pointer to a
  ticket.
- **No comments that restate what the line already says.**
