FROM elasticsearch:2.2.0

RUN /usr/share/elasticsearch/bin/plugin install delete-by-query

CMD chown -R elasticsearch:elasticsearch /usr/share/elasticsearch/data && gosu elasticsearch elasticsearch
