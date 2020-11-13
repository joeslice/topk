# topk

This command line utility takes standard input, splits by line and counts them, reporting
the unique lines preceded by their counts. It is roughly the same as `... | sort | uniq -c | sort -nr`
but uses more efficient data structures internally.


### Usage

```
usage: topk [-h] [--count COUNT] [--capacity CAPACITY] [--failOnFirstError] [--csv]

optional arguments:
  -h, --help             show this help message and exit
  --count COUNT, -n COUNT
                         Number to show, default 1 million
  --capacity CAPACITY    Capacity of the data structure, default 10 million
  --failOnFirstError     Fail fast when reporting errors, default false
  --csv                  Output csv
```

### Building

Assuming maven and java, just execute `mvn clean package` from the root. The executable jar is then within `target/` and can be executed as `java -jar target/topk-1.0-SNAPSHOT.jar`.

I have an alias in my shell startup file (`.zshrc` for me): 
```
alias topk="java -cp ~/work/topk/target/topk-1.0-SNAPSHOT.jar io.github.joeslice.topk.TopK"
```

## Timing test

I grabbed a few files of random words from http://www.thegrammarlab.com/?nor-portfolio=1000000-word-sample-corpora and put one word per line:

```
# file of 3MM words with every 3rd and 20th line repeated
sed 's/ /\n/g' ~/Downloads/sample*.txt | awk '{ if (NR % 20 == 0) { print "every twentieth line" } else if (NR % 3 == 0) { print "every third line" } else { print } }' > samples.txt
$ wc -l samples.txt 
3000005 samples.txt
```

My old way of counting lines:

```
time cat samples.txt| sort | uniq -c | sort -nr | head -n12
 950001 every third line
 150000 every twentieth line
 125921 the
  66959 of
  56842 and
  50585 to
  39602 in
  39332 a
  25152 that
  21913 is
  17247 for
  15912 as
cat samples.txt  0.02s user 0.00s system 0% cpu 2.236 total
sort  2.49s user 0.06s system 99% cpu 2.552 total
uniq -c  0.09s user 0.00s system 3% cpu 2.551 total
sort -nr  0.06s user 0.00s system 2% cpu 2.602 total
head -n12  0.00s user 0.00s system 0% cpu 2.601 total
```

Using topk from this repo:
```
time cat samples.txt| topk --count 12
950001 every third line
150000 every twentieth line
125921 the
66959 of
56842 and
50585 to
39602 in
39332 a
25152 that
21913 is
17247 for
15912 as
cat samples.txt  0.00s user 0.03s system 5% cpu 0.534 total
java -cp ~/work/topk/target/topk-1.0-SNAPSHOT.jar io.github.joeslice.topk.Top  0.84s user 0.12s system 175% cpu 0.544 total
```
