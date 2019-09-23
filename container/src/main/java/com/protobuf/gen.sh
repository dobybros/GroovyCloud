rm -rf ./output
rm -rf ./objcoutput

mkdir ./output
mkdir ./objcoutput

protoc --proto_path=./ --java_out=./output ./*.proto
protoc --proto_path=./ --objc_out=./objcoutput ./*.proto