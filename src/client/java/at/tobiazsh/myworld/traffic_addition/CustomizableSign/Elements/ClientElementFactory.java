package at.tobiazsh.myworld.traffic_addition.CustomizableSign.Elements;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.*;

public class ClientElementFactory {

    public static ClientElementInterface toClientElement(BaseElement globalElement) {
        switch (globalElement) {

            case OnlineImageElement OIE -> {
                OnlineImageElementClient OIEC = new OnlineImageElementClient(
                        OIE.getX(), OIE.getY(),
                        OIE.getWidth(), OIE.getHeight(),
                        OIE.getFactor(),
                        OIE.getRotation(),
                        OIE.getPictureReference(),
                        OIE.getId(), OIE.getParentId()
                );

                OIEC.setName(OIE.getName());
                OIEC.setColor(OIE.getColor());

                return OIEC;
            }

            case TextElement TE -> {
                TextElementClient TEC = new TextElementClient(
                        TE.getX(), TE.getY(),
                        TE.getWidth(), TE.getHeight(),
                        TE.getRotation(),
                        TE.getFactor(),
                        false,
                        TE.getFont(),
                        TE.getText(),
                        TE.getId(), TE.getParentId()
                );

                TEC.setName(TE.getName());
                TEC.setColor(TE.getColor());

                return TEC;
            }

            case GroupElement GE -> {
                GroupElementClient GEC = new GroupElementClient(
                        GE.getX(), GE.getY(),
                        GE.getWidth(), GE.getHeight(),
                        GE.getRotation(),
                        GE.getId(), GE.getParentId()
                );

                GEC.setName(GE.getName());
                GEC.setColor(GE.getColor());
                GEC.setClientElements((GE.getElements().stream().map(ClientElementFactory::toClientElement).toList()));

                return GEC;
            }

            case ImageElement IE -> {
                ImageElementClient IEC = new ImageElementClient(
                        IE.getX(), IE.getY(),
                        IE.getWidth(), IE.getHeight(),
                        IE.getFactor(),
                        IE.getRotation(),
                        IE.getResourcePath(),
                        IE.getId(),
                        IE.getParentId()
                );

                IEC.setName(IE.getName());
                IEC.setColor(IE.getColor());

                return IEC;
            }

            default -> {
                MyWorldTrafficAddition.LOGGER.error("Unknown element: {}", globalElement);
                return null;
            }
        }
    }

    public static BaseElement toGlobalElement(ClientElementInterface clientElement) {
        switch (clientElement) {
            case OnlineImageElementClient OIEC -> {
                return OIEC;
            }

            case TextElementClient TE -> {
                return TE;
            }

            case GroupElementClient GEC -> {
                GroupElement globalGroup = new GroupElement(
                        GEC.getX(), GEC.getY(),
                        GEC.getWidth(), GEC.getHeight(),
                        GEC.getRotation(),
                        GEC.getParentId(),
                        GEC.getId()
                );

                globalGroup.setName(GEC.getName());
                globalGroup.setColor(GEC.getColor());
                globalGroup.setElements(GEC.unpackClient().stream().map(ClientElementFactory::toGlobalElement).toList()); // Unpack client elements to global elements
                return globalGroup;
            }

            case ImageElementClient IE -> {
                return IE;
            }

            default -> {
                MyWorldTrafficAddition.LOGGER.error("Unknown client element: {}", clientElement);
                return null;
            }
        }
    }
}
