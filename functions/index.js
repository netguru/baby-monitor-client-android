const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.sendNotification = functions.database.ref('/notifications/{pushId}')
    .onWrite((change, context) => {
        const message = change.after.val();
        const promises = [];

        if (message) {
            const payload = {
                "notification": {
                    "title": message.title,
                    "body": message.body
                },
                "token": message.to
            };

            admin.messaging().send(payload)
                .catch(error => {
                    console.error('Error sending message:', error);
                });
            promises.push(change.after.ref.remove());
        }

        return Promise.all(promises);
    });
