FROM anapsix/alpine-java

ARG vcs_url="Unknown"
ARG vcs_ref="Unknown"
ARG vcs_branch="Unknown"
ARG build_date="Unknown"

LABEL org.label-schema.vcs-type=git \
      org.label-schema.vcs-url=$vcs_url \
      org.label-schema.vcs-ref=$vcs_ref \
      org.label-schema.vcs-branch=$vcs_branch \
      org.label-schema.docker.dockerfile=/Dockerfile \
      org.label-schema.build-date=$build_date

EXPOSE 8000

# Add Tini
ENV TINI_VERSION v0.10.0
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini-static /tini
RUN chmod +x /tini

ENTRYPOINT ["/tini", "--"]
CMD ["/code/bin/hello2"]

COPY ./Dockerfile /Dockerfile
ADD ./build/install/* /code