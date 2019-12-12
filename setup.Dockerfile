FROM python:3.7-alpine
COPY . /home/src
WORKDIR /home/src
RUN pip3 install requests

ENTRYPOINT ["python", "setup.py", "--docker"]
