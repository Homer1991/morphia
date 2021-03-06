#! /bin/sh

cat .travis.template.yml > .travis.yml

for MONGODB in 4.2.8 4.0.19 3.6.18
do
  for DRIVER in 4.0.4 3.12.5 3.11.2 3.10.2 3.9.1 3.8.2 3.7.1
  do
    echo "  - MONGODB=$MONGODB DRIVER=$DRIVER" >> .travis.yml
  done
done
