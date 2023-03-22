import json
from wap_actions.core.configuration import UrlNotificationConfig


class Matcher:

    def matches(self, event_json):
        return True


class EntityTypeMatcher(Matcher):

    def matches(self, event_json):
        if self.config.entity_types is not None and len(self.config.entity_types) > 0:
            entity_type = event_json.get("entityType")
            if entity_type is None or entity_type not in self.config.entity_types:
                return False, None
        return True, None

    def __init__(self, config: UrlNotificationConfig):
        self.config = config


class PropValueMatcher(EntityTypeMatcher):

    def matches(self, event_json):
        if not super().matches(event_json):
            return False, None
        if self.config.entity_props is not None and len(self.config.entity_props) > 0:
            if self.aspect_name != event_json.get("aspectName"):
                return False, None
            aspect = event_json.get("aspect")
            if aspect is None or "value" not in aspect.keys():
                return False, None
            props = json.loads(aspect.get("value").replace("\\", "")).get("customProperties")
            return self._match_value(props=props), props
        return True, None

    def _match_value(self, props: dict):
        for each_prop in self.config.entity_props:
            pos = each_prop.index("=")
            key = each_prop[0:pos].strip()
            value = each_prop[pos+1:len(each_prop)].strip()
            if key not in props.keys() or props[key] == "":
                return False
            if value != "*" and props[key] != value:
                return False
        return True

    def __init__(self, config: UrlNotificationConfig, aspect_name: str):
        super().__init__(config=config)
        self.aspect_name = aspect_name
