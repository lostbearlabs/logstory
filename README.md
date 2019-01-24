# LogStory

## Overview

LogStory is  a tool for analyzing log files.  Like `grep` it lets you search for regular expressions.  It enhances this search by letting you correlate related log lines.


## Tutorial: Dogs 

Consider the following log file, which describes two dogs:

```
# text file 'dog-story.txt'
Watch the dogs run.
Spot is a Dalmation.
Bear is a Boxer.
See Spot run.
Spot has a red ball.
See Bear run.
Bear has a blue ball.
```

If we want to separate the stories of the three dogs we can tell LogStory how they are structured using a configuration like this:

```
# configuration 'dog-config'

start: (?<name>\w+) is a (?<breed>\w)
match, required: See (?<name>\w+) run
end: (?<name>\w+) has a (?<color>w+) ball

```

We can then run LogStory:

```
$> logstory dog-config dog-story.txt
```

LogStory will identify each unique start line and then list all the related stories in the log file:

```
--- name=Spot
Spot is a Dalmation.
See Spot run.
Spot has a red ball.

-- name=Bear
Bear is a Boxer.
See Bear run.
Bear has a blue ball.
```


## Config Files

### Comments

Lines that begin with `#` are ignored.


### Patterns

Patterns are specified with a line that looks like this:

```
action[, action...]: regex
```

The `regex` is just a regex.  It may contain named fields.

The possible values for `action` are:

* `start` - a line matching this pattern marks the beginning of a story.
* `restart` - a line matching this pattern marks the beginning of a story and also treats any other stories in progress as ended.  Use this for files where stories don't overlap each other.
* `end` - a line matching this pattern marks the end of a story.
* `match` - a line matching this pattern can be part of a story but is neither the beginning nor the end.
* `required` - at least one line matching this pattern must be part of every story.  This can be combined with `end` or `match`.

### Regular Expressions

The regex's in the config file use Java's regular expression syntax.

## Stories

### When do Stories Start?

Every story begins with a line that matches a `start` patterns in the config.  There can be multiple `start` patterns specified if the story can start in different ways.

### When do Stories End?

If there are patterns that are marked both `required` and `end` then every story must end with one of those patterns.

Otherwise, a story ends when:
* an `end` action is encountered
* a `restart` action is encountered
* the end of the log file is reached

### What lines go into a story?

If the regex's in your config file don't have any named fields, then this is easy:  A story is a sequential subset of your logfile that starts matches the patterns you've specified.

When your regex's *do* contained named fields, then a new filter comes into play:
* the first time a story contains that field in a line, the value is recorded
* subsequent lines with the same field must have the same value

If the `start` of a story contains a named field, then every matching line indicates the beginning of a new story.

### Filtering Stories

You can limit which stories are printed by adding `filter` directives to your configuration:

```
filter name Spot
filter color Blue
```

Filter lines have the syntax:

```
filter field value
```
