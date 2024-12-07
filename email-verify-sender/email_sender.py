import logging
from os import getenv
import smtplib
from email.message import EmailMessage


def send_email(recipient: str, code: str, url: str):
    logger = logging.getLogger("email_sender")

    sender = getenv("SENDER_EMAIL_ADDRESS")
    password = getenv("SENDER_PASSWORD")

    body = f"<p>Please verify your account by clicking the link below.</p><a href=\"{url}\">{url}</a><p>Verification code: {code}</p>"
    msg = EmailMessage()
    msg["Subject"] = "Email Verification"
    msg["From"] = sender
    msg["To"] = recipient
    msg.set_content(body, subtype="html")
    try:
        logger.info("Sending email")
        with smtplib.SMTP_SSL("smtp.gmail.com", 465) as smtp_server:
            smtp_server.login(sender, password)
            smtp_server.sendmail(sender, recipient, msg.as_string())

            logger.info("Email sent")
    except Exception as e:
        logger.error(f"Unable to send email", exc_info=e)
